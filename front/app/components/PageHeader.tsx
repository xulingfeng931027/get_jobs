"use client"
import { ReactNode } from 'react'
import { motion } from 'framer-motion'

export default function PageHeader({
  icon,
  title,
  subtitle,
  iconClass = 'text-primary',
  accentBgClass = 'bg-primary/10 dark:bg-primary/20',
  actions,
}: {
  icon: ReactNode
  title: string
  subtitle?: string
  iconClass?: string
  accentBgClass?: string
  actions?: ReactNode
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: "easeOut" }}
      className="mb-8"
    >
      <div className="flex items-center gap-4 p-6 rounded-2xl bg-card border border-border/50 shadow-bento">
        <motion.div
          initial={{ scale: 0.8, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ delay: 0.15, duration: 0.3, ease: "easeOut" }}
          className={`p-4 rounded-xl ${accentBgClass} shadow-bento-sm`}
        >
          <span className={`${iconClass} text-2xl`}>{icon}</span>
        </motion.div>
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.3, duration: 0.5 }}
          className="flex-1"
        >
          <h1 className="text-3xl font-bold tracking-tight text-foreground">
            {title}
          </h1>
          {subtitle && (
            <p className="text-waterloo dark:text-manatee mt-1.5 text-base">
              {subtitle}
            </p>
          )}
        </motion.div>
        {actions && (
          <motion.div
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.4, duration: 0.3 }}
            className="ml-auto flex items-center gap-2"
          >
            {actions}
          </motion.div>
        )}
      </div>
    </motion.div>
  )
}